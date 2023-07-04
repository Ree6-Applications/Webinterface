<script lang="ts">
  import { goto } from "$app/navigation";
  import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
  import { currentServer, type Server, serversLoading } from "$lib/scripts/servers";

    export let description: String;
    export let servers: Server[];

    function selectServer(server: Server) {

        if(!server.setup) {
            location.assign("https://invite.ree6.de")
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
        <img src="{server.icon}" class="material-icons icon-replacer icon-primary" alt="hi">
        <h3>{server.name}</h3>

        {#if server.setup}

        <div on:click={() => selectServer(server)} on:keydown class="button clickable">
            <span class="material-icons icon-primary">edit</span>
            <p>Manage</p>
        </div>

        {:else}

        <div on:click={() => location.assign("https://invite.ree6.de")} on:keydown class="button clickable">
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
</style>

