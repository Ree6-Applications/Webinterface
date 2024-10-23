<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();

    interface Props {
        current?: string | null;
        callback: (value: string | null) => void;
        unit?: string;
        picking?: boolean;
    }

    let {
        current = $bindable(null),
        callback,
        unit = "",
        picking = $bindable(false)
    }: Props = $props();

</script>

{#if picking}
<div class="flex">
    <input bind:value={current} placeholder="Anything" />
    <span class="material-icons button icon-primary icon-button better-hover" onclick={() => {
        picking = false;
        if(current == "") {
            current = null;
        }
        callback(current);
    }} onkeydown={bubble('keydown')}>check</span>
</div>
{:else}
<div class="chip chip-hover clickable" onclick={() => {
    picking = true;
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