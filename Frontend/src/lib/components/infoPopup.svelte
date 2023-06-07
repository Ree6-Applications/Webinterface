<script lang="ts">
    import { fade, scale } from "svelte/transition";

    export let title: string;
    export let content: string;
    export let zIndex = 100;
    export let close: () => void;

</script>

<div out:fade={{duration: 250}} in:fade={{duration: 250}} class="dialog-outer" style="z-index: {zIndex};">
    <div out:scale={{start: 0.8, duration: 250}} in:scale={{start: 0.8, duration: 250}} class="dialog">

        <div class="header">
            {#if content != ""}
            <h2>{title}</h2>
            {:else}
            <p class="text-large">{title}</p>
            {/if}
            <span on:click={close} on:keydown class="material-icons icon-medium clickable hover-primary">close</span>
        </div>

        {#if content != ""}
        <div class="content">
            {#each content.split("%nl") as line}
            <p>{line}</p>
            {/each}
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/dialog.scss';

</style>
