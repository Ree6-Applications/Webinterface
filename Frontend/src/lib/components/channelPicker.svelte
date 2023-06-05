<script lang="ts">
    import { currentChannels, type Channel } from "$lib/scripts/servers";
    import { onDestroy, onMount } from "svelte";
    import { fade, scale } from "svelte/transition";

    let channels: Channel[] = []
    let sub = currentChannels.subscribe((entities) => {
        for(let entity of entities) {
            if(entity.type != "TEXT") continue;
            channels.push(entity);
        }
    })

    onDestroy(() => sub());

    export let current: Channel;
    export let message: string;
    export let callback: (id: Channel) => void;

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
                {#each channels as channel}
                <div on:click={() => callback(channel)} on:keydown 
                    class="channel clickable {current.id == channel.id ? 'selected' : ''}">
                    <span class="material-icons icon-primary icon-small">tag</span>
                    <div class="name">{channel.name}</div>
                </div>
                {/each}
                <div on:click={() => callback({
                    id: "-1",
                    name: "hi",
                    type: "TEXT"
                })} on:keydown 
                    class="channel clickable {current.id == null ? 'selected' : ''}">
                    <span class="material-icons icon-primary icon-small">close</span>
                    <div class="name">None</div>
                </div>
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
