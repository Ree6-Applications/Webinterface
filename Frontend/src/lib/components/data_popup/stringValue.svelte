<script lang="ts">

    export let current: string | null = null;
    export let callback: (value: string | null) => void;
    export let unit = "";
    export let picking = false;

</script>

{#if picking}
<div class="flex">
    <input bind:value={current} placeholder="Anything" />
    <span class="material-icons button icon-primary icon-button better-hover" on:click={() => {
        picking = false;
        if(current == "") {
            current = null;
        }
        callback(current);
    }} on:keydown>check</span>
</div>
{:else}
<div class="chip chip-hover clickable" on:click={() => {
    picking = true;
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