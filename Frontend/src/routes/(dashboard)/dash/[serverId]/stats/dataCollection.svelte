<script lang="ts">
  import { page } from "$app/stores";
  import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
  import { get_js } from "$lib/scripts/constants";
  import { onMount } from "svelte";

    let loading = true;
    let optOut = true;

    onMount(() => reload());

    async function reload() {
        loading = true;
        const res = await get_js("/guilds/" + $page.params.serverId + "/opt-out/check")
        loading = false;
        
        console.log(res.message)

        if(res.message == "optedIn") optOut = false;
        else optOut = true;
    }

</script>

<h2 class="headline">Data collection</h2>

<div class="box default-margin">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">analytics</span>
                <h1 class="text-medium">Data opt-out</h1>
            </div>
            <p class="text-bg">Opt-out of data collection and monitoring.</p>
        </div>

        {#if loading}
        <div class="loading">
            <LoadingIndicator size="45" />
        </div>
        {:else}
        <div class="button-bar">

            <div on:click={async () => {
                if(loading) return;
                loading = true;

                const res = await get_js("/guilds/" + $page.params.serverId + "/opt-out")
                console.log(res)
                reload();

            }} class="button">
                <span class="material-icons icon-small icon-primary">{optOut ? "insights" : "logout"}</span>
                <p>{optOut ? "Opt-in" : "Opt-out"}</p>
            </div>
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import "$lib/styles/box.scss";
    @import "$lib/default.scss";

</style>