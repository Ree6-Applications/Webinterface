<script lang="ts">
    import StringPicker from "../stringPicker.svelte";

    export let current: string | null = null;
    export let strings: string[] = []
    export let callback: (value: string | null) => void;
    export let picking = false;

</script>

{#if picking}
<StringPicker nullable={false} strings={strings.map((value, _) => value.split(":")[1])} current={""} zIndex={200} message="Select a value." callback={(role) => {
    picking = false;

    // Grab id
    let id = strings.filter((value, _) => value.split(":")[1] == role)
    console.log(id)
    current = id[0].split(":")[0]

    callback(current);
}} />
{/if}

<div class="chip chip-hover clickable" on:click={() => {
    picking = true;
}} on:keydown>
    {#if current != null}
    <p class="text-small">{strings.filter((value, _) => value.split(":")[0] == current)[0].split(":")[1]}</p>
    {:else}
    <span class="material-icons icon-primary">close</span>
    <p class="text-small">Nothing</p>
    {/if}
</div>

<style lang="scss">
    @import '$lib/styles/chip.scss';
</style>