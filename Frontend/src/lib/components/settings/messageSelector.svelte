<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import LoadingIndicator from "../loadingIndicator.svelte";
    import { setting, updateSetting } from "$lib/scripts/settings";
    import { currentServer } from "$lib/scripts/servers";
    import InfoPopup from "../infoPopup.svelte";

    interface Props {
        title: string;
        icon: string;
        description: string;
        settingName: string;
        formattingDirectives?: string | undefined;
    }

    let {
        title,
        icon,
        description,
        settingName,
        formattingDirectives = undefined
    }: Props = $props();

    let editing = $state(false);
    let showFormatting = $state(false);
    let current = setting(settingName);
    let store = current.value;

    current.value.subscribe((value) => {
        console.log(value)
    })

</script>

{#if showFormatting}
<InfoPopup title="Formatting" content={formattingDirectives ?? ""} close={() => showFormatting = false} />
{/if}

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

            {#if !editing}
            <div class="text paragraph">
                {#each $store.split("\n") as line}
                <p>{line}</p>
                {/each}
            </div>
            {:else}
            <textarea bind:value={$store} placeholder="Any word"></textarea>
            {/if}

            <div onclick={() => {
                if(!editing) {
                    editing = true
                } else {
                    updateSetting(settingName, $currentServer.id + "", $store)
                    editing = false
                }
            }} onkeydown={bubble('keydown')} class="button icon-button">
                <span class="material-icons icon-small icon-primary">{editing ? "check" : "edit"}</span>
            </div>

            {#if formattingDirectives}
            <div onclick={() => showFormatting = true} onkeydown={bubble('keydown')} class="button icon-button">
                <span class="material-icons icon-small icon-primary">info</span>
            </div>
            {/if}
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
</style>
