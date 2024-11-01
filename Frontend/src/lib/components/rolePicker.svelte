<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import { currentRoles, type Role } from "$lib/scripts/servers";
    import { fade, scale } from "svelte/transition";

    interface Props {
        current: Role | null;
        message: string;
        zIndex?: number;
        nullable?: boolean;
        callback: (role: Role | null) => void;
    }

    let {
        current,
        message,
        zIndex = 100,
        nullable = false,
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
                {#each $currentRoles as role}
                <div onclick={() => callback(role)} onkeydown={bubble('keydown')} 
                    class="channel clickable {current == role ? 'selected' : ''}">
                    <span class="material-icons icon-small" style={"color: #" + role.color.toString(16) + ";"}>military_tech</span>
                    <div class="name">{role.name}</div>
                </div>
                {/each}
                {#if nullable}
                <div onclick={() => callback(null)} onkeydown={bubble('keydown')} 
                    class="channel clickable {current == null ? 'selected' : ''}">
                    <span class="material-icons icon-small icon-primary">close</span>
                    <div class="name">Nothing</div>
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
            padding: 0.2rem;
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
