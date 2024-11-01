<script lang="ts">
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { get, get_js } from "$lib/scripts/constants";
    import { currentServer, currentError, currentLoading, currentChannels, currentRoles } from "$lib/scripts/servers";
    import { onDestroy, onMount } from "svelte";
  import DataCollection from "./dataCollection.svelte";

    let actions = [
        {
            icon: 'dynamic_feed',
            title: 'Events & logging',
            description: 'Logging settings, auto roles',
            link: '/events'
        },
        {
            icon: 'gavel',
            title: 'Moderation',
            description: 'Moderation features, Enabled commands',
            link: '/moderation'
        },
        {
            icon: 'confirmation_number',
            title: 'Tickets & tools',
            description: 'Tickets system, suggestion system',
            link: '/systems'
        },
        {
            icon: 'leaderboard',
            title: 'Leaderboard',
            description: 'Chat leaderboard, Voice leaderboard',
            link: '/leaderboards'
        }
    ]

</script>

<svelte:head>
    <title>Statistics - { $currentServer.name }</title>
</svelte:head>

<div class="middle">
    <div class="server-profile">
        <img class="profile-img" src={$currentServer.icon ?? "hi"} alt="hi" />
        <h1>{$currentServer.name}</h1>
    </div>
    

    {#if !$currentLoading}
    <div class="stats">
        <div class="stat">
            <span class="material-icons icon-primary icon-medium">tag</span>
            <p>{$currentChannels.length} channels or categories</p>
        </div>
    
        <div class="stat">
            <span class="material-icons icon-primary icon-medium">military_tech</span>
            <p>{$currentRoles.length} roles</p>
        </div>
    </div>
    {:else}
    <LoadingIndicator size="50" error={$currentError} />
    {/if}
</div>

<h2 class="headline">Bot settings</h2>

<div class="actions">

    {#each actions as action}
    <div class="action" on:click={() => {

        // Go to link
        goto("/dash/" + $page.params["serverId"] + action.link)
    }} on:keydown={() => {}}>
        <div class="content">
            <span class="material-icons icon-primary icon-large">{action.icon}</span>
            <div class="text">
                <h1 class="text-medium">{action.title}</h1>
                <p class="text-bg">{action.description}</p>
            </div>
        </div>
        <span class="material-icons icon-large">arrow_forward_ios</span>
    </div>
    {/each}
</div>

<DataCollection />

<style lang="scss">

    @import "$lib/styles/box.scss";

    .profile-img {
        border-radius: 20rem;
        width: 7vw;
        aspect-ratio: 1/1;
    }

    .middle {
        display: flex;
        justify-content: center;
        flex-direction: column;
        height: 40%;
    }

    .server-profile {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;

        h1 {
            font-size: 5rem;
        }

        span {
            font-size: 10rem;
        }
    }
    
    .headline {
        margin-top: 1.5rem;
        margin-bottom: 0.8rem;
    }

    .stats {
        display: flex;
        justify-content: center;
        padding: 0.5rem;
        gap: 0.5rem;
        flex-wrap: wrap;

        .stat {
            display: flex;
            gap: 0.3rem;
            align-items: center;
            padding: 0.6rem;
            border-radius: 1rem;
            font-size: 1.2rem;
            background-color: var(--outer-space);
        }
    }

    span {
        user-select: none;
    }

    .actions {
        display: flex;
        gap: 1.3rem;
        flex-wrap: wrap;
        
        .action {
            user-select: none;
            display: flex;
            cursor: pointer;
            gap: 0.7rem;
            align-items: center;
            justify-content: space-between;
            width: calc(50% - 2.65rem);
            padding: 1rem;
            border-radius: 1rem;
            transition: 250ms all ease;
            background-color: var(--outer-space);
            
            .content {
                display: flex;
                gap: 0.7rem;
                align-items: center;

                .text {
                    display: flex;
                    flex-direction: column;
                    gap: 0.3rem;
                }
            }

            &:hover {
                box-shadow: 0px 0.25rem 0px 0px var(--eerie-black);
                transform: translateY(-0.25rem);
            }
        }
    }

    @media (max-width: 800px) {

        .middle {
            display: flex;
            justify-content: center;
            flex-direction: column;
            height: auto;
            padding: 1rem;
        }

        .profile-img {
            border-radius: 20rem;
            width: 35px;
            aspect-ratio: 1/1;
        }

        .server-profile {
            display: flex;
            align-items: center;
            gap: 0.5rem;

            h1 {
                font-size: 1.5rem;
            }
        }

        .stats .stat {
            font-size: 1rem;
        }

        .actions {
            flex-wrap: nowrap;
            flex-direction: column;

            .action {
                width: auto;
            }
        }
    }

</style>