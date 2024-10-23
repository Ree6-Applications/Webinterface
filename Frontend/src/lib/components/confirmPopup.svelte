<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import { fade, scale } from "svelte/transition";

    interface Props {
        title: string;
        content: string;
        zIndex?: number;
        close: (confirmed: boolean) => void;
    }

    let {
        title,
        content,
        zIndex = 100,
        close
    }: Props = $props();

</script>

<div out:fade={{duration: 250}} in:fade={{duration: 250}} class="dialog-outer" style="z-index: {zIndex};">
    <div out:scale={{start: 0.8, duration: 250}} in:scale={{start: 0.8, duration: 250}} class="dialog">

        <div class="header">
            <h2 class="text-large">{title}</h2>
            <span onclick={() => close(false)} onkeydown={bubble('keydown')} class="material-icons icon-medium clickable hover-primary">close</span>
        </div>

        <div class="content text-small">
            {#each content.split("%nl") as line}
            <p>{line}</p>
            {/each}
        </div>

        <div class="buttons">
            <button class="text-medium" onclick={() => close(true)}>Yes</button>
            <button class="text-medium" onclick={() => close(false)}>No</button>
        </div>
    </div>
</div>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/dialog.scss';

    .buttons {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 0.5rem;

        button {
            display: flex;
            align-items: center;
            gap: var(--button-gap);
            padding: var(--button-padding);
            color: var(--text-color);
            background-color: var(--onyx);
            border-radius: 1rem;
            border: none;
            transition: 250ms all ease;
            cursor: pointer;

            &:hover {
                background-color: var(--outer-space);
            }
        }
    }

</style>
