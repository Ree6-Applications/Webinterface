<script lang="ts">
    import LoadingIndicator from "../loadingIndicator.svelte";
    import { setting, updateSetting } from "$lib/scripts/settings";
    import { currentServer } from "$lib/scripts/servers";

    export let title: string;
    export let icon: string;
    export let description: string;
    export let settingName: string;

    let current = setting(settingName);
    let store = current.value;

    current.value.subscribe((value) => {
        console.log(value)
    })

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

        {#if $store == ":loading"}
        <div class="loading">
            <LoadingIndicator size="45" />
        </div>
        {:else}
        <div class="button-bar">

            <div on:click={() => {
                updateSetting(settingName, $currentServer.id + "", $store == "true" ? "false" : "true")
            }} class="button">
                <span class="material-icons icon-small icon-primary">{$store == "true" ? "close" : "check"}</span>
                <p>{$store == "true" ? "Disable" : "Enable"}</p>
            </div>
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
</style>
