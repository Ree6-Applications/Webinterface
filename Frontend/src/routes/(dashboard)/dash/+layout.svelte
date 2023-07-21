<script lang="ts">
    import { onMount } from "svelte";

    import Sidebar from "./sidebar.svelte";
    import { currentServer, loadServers, servers, serversLoading } from "$lib/scripts/servers";
    import { page } from "$app/stores";
    import { fade } from "svelte/transition";
    import { goto } from "$app/navigation";
    import ServerSelector from "./serverSelector.svelte";

    let expandedSidebar = false;

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
            admin: false,
            setup: false
        });
    })

</script>

<div class="space">
    <div class="title">
        <ServerSelector menuButton={true} bind:expandedSb={expandedSidebar} />
    </div>

    <div class="body">
        <div class="sidebar {expandedSidebar ? "sidebar-expanded" : "sidebar-hide"}">
            <Sidebar callback={() => expandedSidebar = false} />
        </div>
    
        {#if !$serversLoading}
        <div in:fade class="content">
            <slot />
            <div class="spacer"></div>
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    .sidebar {
        background-color: var(--eerie-black);
        width: 100%;
        height: 100%;
        max-width: 350px;
        overflow-y: scroll;
    }

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
        height: 100%;
        overflow-y: scroll;
        padding: 0rem 2rem;

        .spacer {
            margin-bottom: 2rem;
        }
    }

    .space {
        display: flex;
        flex-direction: column;

        .title {
            display: none;
        }
    }

    @media (max-width: 1300px) {

        .sidebar {
            position: absolute;
            width: 100vw;
            z-index: 200;
            transition: 250ms all ease;
        }

        .sidebar-hide {
            transform: translateX(-100%);
        }

        .sidebar-expanded {
            transform: translateX(0%);
        }

        .space .title {
            background-color: var(--eerie-black);
            display: flex;
            flex-direction: column;
        }

        .content {
            padding: 0 4%;

            .spacer {
                margin-bottom: 5rem;
            }
        }

    }
</style>