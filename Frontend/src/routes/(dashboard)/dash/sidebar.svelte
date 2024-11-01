<script lang="ts">
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { servers, serversLoading, currentServer, type Server } from "$lib/scripts/servers";
    import { onMount } from "svelte";
    import { fade, slide } from "svelte/transition";
  import ServerSelector from "./serverSelector.svelte";

    let expanded = false;

    export let callback = () => {};

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
            icon: "confirmation_number",
            name: "Tickets & tools",
            link: "/systems",
        },
        {
            icon: "newspaper",
            name: "News & rewards",
            link: "/news",
        },
        {
            icon: "leaderboard",
            name: "Leaderboards",
            link: "/leaderboards",
        }
    ];

</script>

<div class="selector">
    <ServerSelector />
</div>

{#if $currentServer.id != 0 && $page.url.pathname.startsWith("/dash/" + $currentServer.id)}
<div class="element-list">

    {#if $currentServer.admin}
    {#each elements as element}
    <div in:fade class="element {$page.url.pathname.startsWith("/dash/" + $currentServer.id + element.link) ? "element-selected" : ""}" on:click={() => {
        goto("/dash/" + $currentServer.id + element.link)
        callback();
    }} on:keydown>
        <span class="material-icons icon-medium icon-primary">{element.icon}</span>
        <p class="text-medium">{element.name}</p>
    </div>
    {/each}

    {:else}

    <div in:fade class="element {$page.url.pathname.startsWith("/dash/" + $currentServer.id + "/settings") ? "element-selected" : ""}" on:click={() => {
        goto("/dash/" + $currentServer.id + "/settings")
        callback();
    }} on:keydown>
        <span class="material-icons icon-medium icon-primary">leaderboard</span>
        <p class="text-medium">Leaderboards & opt-out</p>
    </div>

    {/if}
</div>
{/if}

<style lang="scss">

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

    @media (max-width: 1300px) {
        .selector {
            display: none;
        }
    }
</style>