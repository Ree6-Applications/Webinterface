<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import type { Role } from "$lib/scripts/servers";
    import RolePicker from "../rolePicker.svelte";

    interface Props {
        current?: Role | null;
        callback: (value: Role | null) => void;
        picking?: boolean;
    }

    let { current = $bindable(null), callback, picking = $bindable(false) }: Props = $props();

</script>

{#if picking}
<RolePicker nullable={true} current={current} zIndex={200} message="Select a role." callback={(role) => {
    picking = false;
    current = role;
    callback(role);
}} />
{/if}

<div class="chip chip-hover clickable" onclick={() => {
    picking = true;
}} onkeydown={bubble('keydown')}>
    {#if current != null}
    <span class="material-icons" style={"color: #" + current.color.toString(16) + ";"}>military_tech</span>
    <p class="text-small">{current?.name}</p>
    {:else}
    <span class="material-icons icon-primary">close</span>
    <p class="text-small">Nothing</p>
    {/if}
</div>

<style lang="scss">
    @import '$lib/styles/chip.scss';
</style>