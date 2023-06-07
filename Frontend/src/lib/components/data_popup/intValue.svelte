<script lang="ts">

    export let current: number | null = null;
    export let callback: (value: number | null) => void;
    export let picking = false;
    export let unit = "";

    let writing: string;

</script>

{#if picking}
<div class="flex">
    <input bind:value={writing} placeholder="Any integer" />
    <span class="material-icons button icon-primary icon-button better-hover" on:click={() => {
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
    }} on:keydown>check</span>
</div>
{:else}
<div class="chip chip-hover clickable" on:click={() => {
    picking = true;
    writing = current == null ? "" : current.toString();
}} on:keydown>
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