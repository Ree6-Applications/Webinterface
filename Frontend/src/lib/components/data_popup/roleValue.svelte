<script lang="ts">
    import type { Role } from "$lib/scripts/servers";
    import RolePicker from "../rolePicker.svelte";

    export let current: Role | null = null;
    export let callback: (value: Role | null) => void;
    export let picking = false;

</script>

{#if picking}
<RolePicker nullable={true} current={current} zIndex={200} message="Select a role." callback={(role) => {
    picking = false;
    current = role;
    callback(role);
}} />
{/if}

<div class="chip chip-hover clickable" on:click={() => {
    picking = true;
}} on:keydown>
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