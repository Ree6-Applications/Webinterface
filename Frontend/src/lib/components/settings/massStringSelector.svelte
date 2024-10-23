<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import { onMount } from "svelte";
    import LoadingIndicator from "../loadingIndicator.svelte";
    import { get, post_js } from "$lib/scripts/constants";
    import { slide } from "svelte/transition";

    interface Props {
        icon: string;
        title: string;
        description: string;
        endpoint: string;
    }

    let {
        icon,
        title,
        description,
        endpoint
    }: Props = $props();

    let strings: string[] = $state([]);
    let stringLoading: string | null = $state(null);
    let error = $state(false);
    let loading = $state(true);
    let toAdd = $state("");

    onMount(async () => {

        // Request value
        const res = await get(endpoint);

        if(res.status != 200) {
            error = true;
            console.log(res)
            return;
        }

        const json = await res.json();
        console.log(json)
        json.object.forEach((element: string) => {
            strings.push(element)
        });

        error = false;
        loading = false;

    })

    async function addString(value: string) {
        if(loading) return;

        for(let element of strings) {
            if(element === value) return;
        }

        loading = true;

        // Add role
        const json = await post_js(endpoint + "/add", JSON.stringify({
            "value": value
        }))

        if(!json.success) {
            error = true;

            setTimeout(() => {
                loading = false;
                error = false;
            }, 500);
            return;
        }

        loading = false;

        strings.push(value);
        strings = strings;
    }

</script>

<div class="box">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">{icon}</span>
                <h1 class="text-medium">{title}</h1>
            </div>
            <p class="text-bg">{description}</p>
        </div>

        {#if loading}
        <div class="loading">
            <LoadingIndicator error={error} size="45" />
        </div>

        {:else}


        <div class="button-bar">

            <input placeholder="Any word" bind:value={toAdd} />

            <div onclick={() => addString(toAdd)} onkeydown={bubble('keydown')} class="button icon-button">
                <span class="material-icons icon-small icon-primary">add</span>
            </div>
        </div>

        {/if}
    </div>

    {#if !loading && strings.length > 0}
    <div in:slide class="chips default-margin">
        {#each strings as string}
        <div class="chip">
            <p class="text-small">{string}</p>

            {#if stringLoading != string}
            <span onclick={async () => {
                if(stringLoading != null) return;
                stringLoading = string;

                // Delete role
                const json = await post_js(endpoint + "/remove", JSON.stringify({
                    "value": string
                }))

                console.log(json)
                if(!json.success) {
                    stringLoading = null;
                    return;
                }

                // Remove role from list
                strings = strings.filter((element) => {
                    return element != string;
                })

                stringLoading = null;

            }} onkeydown={bubble('keydown')} class="material-icons icon-primary clickable chip-button">close</span>
            {:else}
            <LoadingIndicator size="1.5rem" />
            {/if}
        </div>
        {/each}
    </div>
    {/if}
</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
    @import '$lib/default.scss';
    @import '$lib/styles/chips.scss';
</style>