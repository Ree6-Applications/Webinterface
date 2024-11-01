<script lang="ts">
  import { serversLoading, type Server, getServers } from "$lib/scripts/servers";
  import { onDestroy } from "svelte";
  import ServerDisplay from "./[serverId]/serverDisplay.svelte";
  import { INVITE_URL } from "$lib/scripts/constants";

    let adminServers: Server[] = [];
    let normalServers: Server[] = [];
    let inviteServers: Server[] = [];

    const sub = serversLoading.subscribe(b => {
        if(!b) {
            adminServers = getServers(true, true)
            normalServers = getServers(true, false)
            inviteServers = getServers(false, true)
        
            // Print sizes
            console.log("Admin servers: " + adminServers.length)
            console.log("Normal servers: " + normalServers.length)
            console.log("Invite servers: " + inviteServers.length)
        }
    })

    onDestroy(() => sub())

</script>

{#if adminServers.length > 0 || normalServers.length > 0 || inviteServers.length > 0}
<ServerDisplay description="Change bot settings" servers={adminServers} />
<ServerDisplay description="Data collection settings & leaderboards" servers={normalServers} />
<ServerDisplay description="Add the bot to other servers" servers={inviteServers} />
{:else}
<div class="center">
    <h2>Seems like the bot isn't on any of your servers.</h2>
    <p class="text-bg">Seeing this probably means you're new on discord! In that case, welcome!</p>
    <a href={INVITE_URL} class="button link hover-primary">
        <span class="material-icons">launch</span>
        <p>Try to invite the bot</p>
    </a>
</div>
{/if}

<style lang="scss">
    @import '$lib/default.scss';    
    @import '$lib/styles/comp.scss';    

    .center {
        width: 100%;
        height: 90%;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        gap: 0.5rem;
    }

</style>