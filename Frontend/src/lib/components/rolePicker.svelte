<script lang="ts">
    import { currentRoles, type Role } from "$lib/scripts/servers";
    import { fade, scale } from "svelte/transition";

    export let current: Role | null;
    export let message: string;
    export let callback: (role: Role | null) => void;

    function close() {
        callback(current);
    }

</script>

<div out:fade={{duration: 250}} in:fade={{duration: 250}} class="dialog-outer">
    <div out:scale={{start: 0.8, duration: 250}} in:scale={{start: 0.8, duration: 250}} class="dialog">

        <div class="header">
            <h2>{message}</h2>
            <span on:click={close} on:keydown class="material-icons icon-medium clickable hover-primary">close</span>
        </div>

        <div class="content">
            <div class="channels">
                {#each $currentRoles as role}
                <div on:click={() => callback(role)} on:keydown 
                    class="channel clickable {current == role ? 'selected' : ''}">
                    <span class="material-icons icon-small" style={"color: #" + role.color.toString(16) + ";"}>military_tech</span>
                    <div class="name">{role.name}</div>
                </div>
                {/each}
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
