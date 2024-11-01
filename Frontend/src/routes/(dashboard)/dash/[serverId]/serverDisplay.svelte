<script lang="ts">
  import { goto } from "$app/navigation";
  import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
  import { currentServer, type Server, serversLoading } from "$lib/scripts/servers";
  import { INVITE_URL } from "$lib/scripts/constants";

    export let description: String;
    export let servers: Server[];

    function selectServer(server: Server) {

        if(!server.setup) {
            location.assign(new URL(INVITE_URL))
            return;
        }

        if(server.admin) {
            goto("/dash/" + server.id + "/stats")
        } else {
            goto("/dash/" + server.id + "/settings")
        }
        
        currentServer.set(server)
    }
</script>

{#if servers.length > 0}
<h1 class="headline">{description}</h1>

<div class="servers">

    {#if !$serversLoading}
    
    {#each servers as server}
    <div class="server">
        <div class="title">
            <img src="{server.icon}" class="material-icons icon-replacer" alt="hi">
            <h3>{server.name}</h3>
        </div>

        {#if server.setup}

        <div on:click={() => selectServer(server)} on:keydown class="button clickable">
            <span class="material-icons icon-primary">edit</span>
            <p>Manage</p>
        </div>

        {:else}

        <div on:click={() => location.assign(new URL(INVITE_URL))} on:keydown class="button clickable">
            <span class="material-icons icon-primary">launch</span>
            <p>Setup</p>
        </div>

        {/if}
    </div>
    {/each}

    {:else}

    <div class="center">
        <LoadingIndicator size="100"/>
    </div>

    {/if}
</div>
{/if}

<style lang="scss">
    @import '$lib/default.scss';

    .icon-replacer {
        aspect-ratio: 1/1;
        width: 100px;
        border-radius: 10rem;
    }

    .servers {
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        gap: 3rem;

        .server {
            display: flex;
            justify-content: center;
            align-items: center;
            flex-direction: column;
            gap: 0.5rem;
            border-radius: 1rem;
            padding: 1rem 2rem;
            min-width: 150px;
            max-width: 17%;
            background-color: var(--outer-space);

            .title {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 0.5rem;
                justify-content: center;
            }

            h3 {
                width: 180px;
                text-align: center;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }

            .button {
                margin-top: 0.1rem;
                display: flex;
                align-items: center;
                gap: var(--button-gap);
                padding: var(--button-padding);
                border-radius: 1rem;
                background-color: var(--onyx);
                transition: 250ms ease;
            
                &:hover {
                    background-color: var(--eerie-black);
                }
            }
        }
    }

    @media (max-width: 600px) {

        .servers {
            flex-wrap: nowrap;
            flex-direction: column;
            gap: 0.5rem;
        }

        .servers .server {
            display: flex;
            flex-direction: row;
            min-width: 0vw;
            max-width: 100vw;
            width: auto;
            align-items: center;
            justify-content: space-between;
            padding: 1rem 1rem;
            
            .title {
                flex-direction: row;
                justify-content: start;
                align-items: center;
                gap: 0.5rem;
            }

            h3 {
                text-align: start;
                white-space: nowrap;
                width: 80%;
                text-overflow: ellipsis;
            }
        }

        .icon-replacer {
            aspect-ratio: 1/1;
            width: 40px;
            border-radius: 10rem;
        }
    }
</style>

