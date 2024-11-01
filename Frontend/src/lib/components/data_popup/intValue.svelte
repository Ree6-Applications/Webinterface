<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();

    interface Props {
        current?: number | null;
        callback: (value: number | null) => void;
        picking?: boolean;
        unit?: string;
    }

    let {
        current = $bindable(null),
        callback,
        picking = $bindable(false),
        unit = ""
    }: Props = $props();

    let writing: string = $state();

</script>

{#if picking}
<div class="flex">
    <input bind:value={writing} placeholder="Any integer" />
    <span class="material-icons button icon-primary icon-button better-hover" onclick={() => {
        picking = false;
        try {
            current = parseInt(writing);
            if(isNaN(current)) {
                current = null;
            }
        } catch {
            current = null;
        }
        callback(current);
    }} onkeydown={bubble('keydown')}>check</span>
</div>
{:else}
<div class="chip chip-hover clickable" onclick={() => {
    picking = true;
    writing = current == null ? "" : current.toString();
}} onkeydown={bubble('keydown')}>
    {#if current == null}
    <span class="material-icons icon-primary">close</span>
    <p class="text-small">Nothing</p>
    {:else}
    <p class="text-small">{current} {unit}</p>
    {/if}
</div>
{/if}

<style lang="scss">
    @import '$lib/styles/chip.scss';
    @import '$lib/styles/comp.scss';

    .flex {
        display: flex;
        align-items: center;
        gap: var(--button-gap);
    }

    .better-hover:hover {
        background-color: var(--outer-space);
    }
</style>