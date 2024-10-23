<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import { fade, scale } from "svelte/transition";

    interface Props {
        current: string | null;
        message: string;
        strings?: string[];
        nullable?: boolean;
        zIndex?: number;
        callback: (selected: string | null) => void;
    }

    let {
        current,
        message,
        strings = [],
        nullable = false,
        zIndex = 100,
        callback
    }: Props = $props();

    function close() {
        callback(current);
    }

</script>

<div out:fade={{duration: 250}} in:fade={{duration: 250}} class="dialog-outer" style="z-index: {zIndex};">
    <div out:scale={{start: 0.8, duration: 250}} in:scale={{start: 0.8, duration: 250}} class="dialog">

        <div class="header">
            <h2>{message}</h2>
            <span onclick={close} onkeydown={bubble('keydown')} class="material-icons icon-medium clickable hover-primary">close</span>
        </div>

        <div class="content">
            <div class="channels">
                {#each strings as string}
                <div onclick={() => callback(string)} onkeydown={bubble('keydown')} 
                    class="channel clickable {current == string ? 'selected' : ''}">
                    <div class="name">{string}</div>
                </div>
                {/each}
                {#if nullable}
                <div onclick={() => callback(null)} onkeydown={bubble('keydown')} 
                    class="channel clickable {current == null ? 'selected' : ''}">
                    <span class="material-icons icon-primary icon-small">close</span>
                    <div class="name">None</div>
                </div>
                {/if}
            </div>
        </div>
    </div>
</div>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/dialog.scss';

    .channels {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;

        .channel {
            padding: 0.5rem;
            background-color: var(--onyx);
            border-radius: 0.5rem;
            display: flex;
            align-items: center;
            transition: 250ms ease;

            &:hover {
                background-color: var(--outer-space);
            }
        }

        .selected {
            background-color: var(--outer-space);
        }
    }

</style>
