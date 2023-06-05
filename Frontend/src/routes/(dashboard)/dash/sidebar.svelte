<script lang="ts">
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { servers, serversLoading, currentServer, type Server } from "$lib/scripts/servers";
  import { onMount } from "svelte";
    import { fade, slide } from "svelte/transition";

    let expanded = false;

    onMount(() => {
        console.log($page.url.pathname)
    })

    let elements = [
        {
            icon: "insights",
            name: "Overview",
            link: "/stats",
        },
        {
            icon: "dynamic_feed",
            name: "Events & logging",
            link: "/events",
        },
        {
            icon: "gavel",
            name: "Moderation",
            link: "/moderation",
        },
        {
            icon: "movie",
            name: "Social media",
            link: "/media",
        },
        {
            icon: "leaderboard",
            name: "Leaderboards",
            link: "/leaderboards",
        },
    ];

    function selectServer(server: Server) {

        if(!server.setup) {
            goto("/");
            currentServer.set(server)
            expanded = false;
            return;
        }

        goto("/dash/" + server.id + "/stats")
        currentServer.set(server)
        expanded = false;
    }

</script>

<div class="sidebar">
    <div class="server-selector">
        <span on:click={() => {
            goto("/dash")
        }} on:keydown class="material-icons icon-large icon-primary middle clickable">face</span>
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
                <div class="server" on:click={() => selectServer(server)} on:keydown>
                    <img src="{server.icon}" class="material-icons img-small" alt="hi">
                    <p class="server-name text-medium">{server.name}</p>
                </div>
                {/each}
            </div>
            {/if}
        
            {:else}

            <LoadingIndicator size="30" />
    
            {/if}
        </div>
    </div>

    {#if $currentServer.id != 0 && $page.url.pathname.startsWith("/dash/" + $currentServer.id)}
    <div class="element-list">

        {#each elements as element}
        <div in:fade class="element {$page.url.pathname.startsWith("/dash/" + $currentServer.id + element.link) ? "element-selected" : ""}" on:click={() => {
            goto("/dash/" + $currentServer.id + element.link)
        }} on:keydown>
            <span class="material-icons icon-medium icon-primary">{element.icon}</span>
            <p class="text-medium">{element.name}</p>
        </div>
        {/each}
    </div>
    {/if}
</div>

<style lang="scss">
    .sidebar {
        background-color: var(--eerie-black);
        width: 100%;
        height: 100%;
        max-width: 350px;
    }

    .img-small {
        width: 32px;
        border-radius: 1rem;
    }

    .server-selector {
        display: flex;
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

    .element-list {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        padding: 0rem 0.6rem 0.6rem 0.6rem;
    }

    .element {
        cursor: pointer;
        display: flex;
        gap: 0.4rem;
        align-items: center;
        padding: 0.5rem;
        border-radius: 0.5rem;
        transition: 250ms all ease;

        &:hover {
            background-color: var(--outer-space);
        }
    }

    .element-selected {
        background-color: var(--outer-space);
    }
</style>