<script lang="ts">
    import { onMount } from "svelte";

    import Sidebar from "./sidebar.svelte";
    import { currentServer, loadServers, servers, type Server, serversLoading } from "$lib/scripts/servers";
    import { page } from "$app/stores";
    import { fade } from "svelte/transition";
    import { goto } from "$app/navigation";

    onMount(async () => {
        await loadServers()

        for(let server of servers.values()) {
            if(server.id == parseInt($page.params.serverId)) {
                currentServer.set(server);
                return;
            }
        }

        let server = servers.get($page.params.serverId);

        if(server == undefined) {
            goto("/dash");
            return;
        }
        
        currentServer.set(servers.get($page.params.serverId) ?? {
            id: 0,
            name: "Unknown server",
            icon: "hi",
            setup: false
        });
    })

</script>
<div class="body">
    <Sidebar />

    {#if !$serversLoading}
    <div in:fade class="content">
        <slot />
    </div>
    {/if}
</div>

<style lang="scss">
    .body {
        padding: 0;
        margin: 0;
        display: flex;
        height: 100vh;
        width: 100vw;
        background-color: var(--onyx);
    }

    .content {
        width: 100%;
        height: calc(100% - 4rem);
        padding: 0rem 2rem;
    }
</style>