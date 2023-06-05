<script lang="ts">
    import { allSettings, setting, settingsLoading } from "$lib/scripts/settings";
    import { onDestroy } from "svelte";
    import LoadingIndicator from "../loadingIndicator.svelte";
    import { slide } from "svelte/transition";
    import { post } from "$lib/scripts/constants";
    import { currentServer } from "$lib/scripts/servers";


    export let title: string;
    export let icon: string;
    export let description: string;
    export let prefix: string;
    
    let loadingFeature = "";
    let loaded = true;
    let loading = false;
    let features: Map<string, CustomSetting> = new Map();

    type CustomSetting = {
        id: string,
        name: string,
        value: string
    }

    let sub = settingsLoading.subscribe(value => {
        if(!value) {
            loaded = false;
            const map = allSettings(prefix);
            map.forEach((value, key) => {
                const unsub = value.value.subscribe((item) => {
                    features.set(key, {
                        id: value.name,
                        name: value.displayName,
                        value: item
                    })
                })
                unsub();
            });
            loaded = true;
        }
    })

    onDestroy(() => {
        sub();
    })

    async function toggle(feature: string, changeLoad: boolean) {
        if(loading && changeLoad) return;
        if(changeLoad) {
            loading = true;
        }
        loadingFeature = feature;
        const newValue = features.get(feature)?.value == "true" ? "false" : "true";

        // Send to server
        const res = await post("/settings/" + $currentServer.id + "/" + feature + "/update", JSON.stringify({
            'value': newValue
        }))

        if(res.status != 200) {
            loading = false;
            return;
        }

        const json = await res.json();
        if(!json.success) {
            console.log(json.message)
            loading = false;
            return;
        }

        setting(feature).value.set(newValue);

        let current = features.get(feature)!;
        current.value = newValue;
        features.set(feature, current);

        features = features;
        loadingFeature = "";
        if(changeLoad) {
            loading = false;
        }
    }

    async function enableAll() {
        if(loading) return;
        loading = true;

        for(let feature of features.values()) {
            if(feature.value == "true") continue;
            await toggle(feature.id, false);
        }

        loading = false;
    }

    async function disableAll() {
        if(loading) return;
        loading = true;

        for(let feature of features.values()) {
            if(feature.value == "false") continue;
            await toggle(feature.id, false);
        }

        loading = false;
    }

</script>

<div class="box default-margin">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">{icon}</span>
                <h1 class="text-medium">{title}</h1>
            </div>
            <p class="text-bg">{description}</p>
        </div>

        {#if !$settingsLoading && !loading}
        <div class="button-bar">
            <div class="button" on:click={enableAll} on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">done_all</span>
                <p class="text-small">Enable all</p>
            </div>

            <div class="button" on:click={disableAll} on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">close</span>
                <p class="text-small">Disable all</p>
            </div>
        </div>

        {:else}

        <LoadingIndicator size="45" />

        {/if}
    </div>

    {#if !$settingsLoading && loaded}
    <div in:slide class="chips default-margin">
        {#each Array.from(features.values()) as feature}
        <div class="chip clickable {feature.value == "true" ? 'chip-enabled' : ''}"
            on:click={() => toggle(feature.id, true)} on:keydown={() => {}}
        >
            {#if loadingFeature == feature.id}
            <LoadingIndicator size="1.8rem"  />
            {:else}
            <span class="material-icons icon-small icon-primary">{feature.value == "true" ? 'done' : 'close'}</span>
            {/if}

            <p class="text-small">{feature.name}</p>
        </div>
        {/each}
    </div>
    {/if}
</div>

<!-- svelte-ignore css-unused-selector -->
<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/box.scss';
    @import '$lib/styles/chips.scss';
</style>