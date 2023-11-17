<script lang="ts">
    import { goto } from "$app/navigation";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { currentServer, serversLoading, type Server, servers } from "$lib/scripts/servers";
    import { slide } from "svelte/transition";
    import { INVITE_URL } from "$lib/scripts/constants";

    let expanded = false;

    export let menuButton = false;
    export let expandedSb = false;

    function selectServer(server: Server) {

        if(!server.setup) {
            location.assign(new URL(INVITE_URL))
            expanded = false;
            return;
        }

        if(server.admin) {
            goto("/dash/" + server.id + "/stats")
        } else {
            goto("/dash/" + server.id + "/settings")
        }
        currentServer.set(server)
        expanded = false;
    }
</script>

<div class="server-selector">

    {#if menuButton}
    <span on:click={() => {
        expandedSb = !expandedSb
    }} on:keydown class="material-icons icon-large icon-primary middle clickable">{expandedSb ? "close" : "menu"}</span>
    {/if}

    <span on:click={() => {
        goto("/dash")
    }} on:keydown class="material-icons icon-large icon-primary middle clickable">apps</span>
    <div class="server-current">

        {#if !$serversLoading}
        <div class="up" on:click={() => {
            expanded = !expanded;
        }} on:keydown>
            <div class="title">
                {#if $currentServer.id == 0}
                <span class="material-icons icon-medium icon-primary">ads_click</span>
                <p class="server-current-name text-medium">Select a server..</p>
                {:else}
                <img src="{$currentServer.icon}" class="material-icons img-small" alt="hi">
                <p class="server-current-name text-medium">{$currentServer.name}</p>
                {/if}
            </div>

            <span class="material-icons icon-medium expand {expanded ? "expand-rotated" : ""}">expand_more</span>
        </div>

        {#if expanded}
        <div in:slide out:slide class="list">
            {#each Array.from(servers.values()) as server}
            {#if server.setup}
            <div class="server" on:click={() => selectServer(server)} on:keydown>
                <img src="{server.icon}" class="material-icons img-small" alt="hi">
                <p class="server-name text-medium">{server.name}</p>
            </div>
            {/if}
            {/each}
        </div>
        {/if}
    
        {:else}

        <LoadingIndicator size="30" />

        {/if}
    </div>
</div>

<style lang="scss">

    .img-small {
        width: 32px;
        border-radius: 1rem;
    }

    .server-selector {
        display: flex;
        align-items: center;
        gap: 0.6rem;
        padding: 0.6rem;
        
        .middle {
            margin-top: 0.25rem;
        }
    }

    .server-current {
        user-select: none;
        display: flex;
        flex-direction: column;
        width: 100%;
        padding: 0.4rem;
        border-radius: 1rem;
        gap: 0.1rem;
        background-color: var(--outer-space);

        .up {
            cursor: pointer;
            display: flex;
            width: 100%;
            padding: 0.1rem;
            align-items: center;
            justify-content: space-between;
            border-radius: 1rem;
            background-color: var(--outer-space);

            .title {
                display: flex;
                align-items: center;
                gap: 0.3rem;
            }
        }

        .server {
            cursor: pointer;
            display: flex;
            margin-top: 0.2rem;
            padding: 0.4rem;
            align-items: center;
            gap: 0.3rem;
            border-radius: 1rem;
            background-color: var(--outer-space);
            transition: 250ms all ease;

            &:hover {
                background-color: var(--onyx);
            }
        }
    }

    .expand {
        user-select: none;
        cursor: pointer;
        transition: 250ms all ease;
    
        &:hover {
            transform: translateY(3px);
        }
    }

    .expand-rotated {
        transform: rotate(180deg);
        
        &:hover {
            transform: rotate(180deg) translateY(3px);
        }
    }

</style>
