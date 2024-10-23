<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import type { Channel } from "$lib/scripts/servers";
    import ChannelPicker from "../channelPicker.svelte";

    interface Props {
        current?: Channel;
        callback: (value: Channel) => void;
        picking?: boolean;
    }

    let { current = $bindable({
        id: null,
        name: null,
        type: "TEXT"
    }), callback, picking = $bindable(false) }: Props = $props();

</script>

{#if picking}
<ChannelPicker current={current} zIndex={200} message="Select a channel." callback={(channel) => {
    picking = false;

    if(channel == undefined) return;

    if(channel.id == "-1") {
        current = {
            id: null,
            name: null,
            type: "TEXT"
        }
    } else {
        current = channel;
    }
    callback(current);
}} />
{/if}

<div class="chip chip-hover clickable" onclick={() => {
    picking = true;
}} onkeydown={bubble('keydown')}>
    {#if current.id != null}
    <span class="material-icons icon-primary">tag</span>
    <p class="text-small">{current.name}</p>
    {:else}
    <span class="material-icons icon-primary">close</span>
    <p class="text-small">Nothing</p>
    {/if}
</div>

<style lang="scss">
    @import '$lib/styles/chip.scss';
</style>