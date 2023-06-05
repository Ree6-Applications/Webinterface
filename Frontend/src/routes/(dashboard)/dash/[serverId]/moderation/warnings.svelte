<script lang="ts">
    import { page } from "$app/stores";
    import ConfirmPopup from "$lib/components/confirmPopup.svelte";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { get_js } from "$lib/scripts/constants";
    import { onMount } from "svelte";

    let loading = true
    let error = false

    let clearAll = false

    onMount(async () => {
        const json = await get_js("/guilds/" + $page.params.serverId + "/warnings")
        
        if(!json.success) {
            error = true
            return
        }
        
        console.log(json)
        loading = false;
    })

</script>

{#if clearAll}
<ConfirmPopup title="Confirm deletion." content="Do you really want to delete all warnings? This can not be undone." close={(b) => {
    clearAll = false;
}} />
{/if}

<div class="box default-margin">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">crisis_alert</span>
                <h1 class="text-medium">Warnings</h1>
            </div>
            <p class="text-bg">The list of all users that have been warned.</p>
        </div>

        {#if !loading}
        <div class="button-bar ns">
            <div class="button" on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">add</span>
                <p class="text-small">Warn someone</p>
            </div>

            <div class="button" on:click={() => clearAll = true} on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">delete</span>
                <p class="text-small">Delete all</p>
            </div>
        </div>
        {:else}
        <LoadingIndicator error={error} size="43" />
        {/if}

    </div>


</div>

<style lang="scss">
    @import '$lib/styles/box.scss';

</style>