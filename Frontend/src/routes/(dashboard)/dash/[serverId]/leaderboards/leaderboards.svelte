<script lang="ts">
    import { page } from "$app/stores";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { get, get_js } from "$lib/scripts/constants";
    import { onMount } from "svelte";
    import { fly, scale, slide } from "svelte/transition";

    let leaderboards: Map<string, any[]> = new Map<string, any[]>()
    let loading = false;

    onMount(async () => {
        loading = true;

        await loadLeaderboard("chat")
        await loadLeaderboard("voice")
        loading = false;
    })

    async function loadLeaderboard(category: string) {
        const json = await get_js("/guilds/" + $page.params.serverId + "/leaderboard/" + category)

        if(!json.success) {
            return;
        }

        console.log(json)

        leaderboards.set(category, category == "chat" ? json.object.chatLeaderboard : json.object.voiceLeaderboard)        
    }

</script>

{#if !loading}

<div in:fly={{y: 100, delay: 500}} class="transition">
    {#each Array.from(leaderboards.keys()) as key}
    <h1 class="headline">{key.substring(0, 1).toUpperCase() + key.substring(1)} Leaderboard</h1>
    
    
    {#each (leaderboards.get(key) ?? []) as leaderboard}
    <div in:fly={{y: 50, delay: 500+(leaderboard.userLevel.rank * 200)}}  class="box default-ct-margin">
        <div class="leaderboard">
            <p class="position icon-primary">#{leaderboard.userLevel.rank}</p>
            
            <div class="stats">
                <div class="user">
                    <img src={leaderboard.user.avatarUrl} alt="Avatar" class="avatar" />
                    <p class="text-large">{leaderboard.user.name}</p>
                </div>
    
                <div class="level">
                    <p class="text-small">Level {leaderboard.userLevel.level} ({leaderboard.userLevel.formattedExperience}/{leaderboard.userLevel.formattedNeededExperience})</p>
                </div>
            </div>
        </div>
    </div>
    {/each}
    
    {/each}    
</div>

{:else}
<div out:scale class="center">
    <LoadingIndicator size="100" />
</div>
{/if}

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/box.scss';

    .center {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .position {
        font-size: 2.5rem;
    }

    .avatar {
        width: 3rem;
        height: 3rem;
        border-radius: 50%;
        margin-right: 0.5rem;
    }

    .leaderboard {
        align-items: center;
        display: flex;
        gap: 0.7rem;

        .stats {
            display: flex;
            flex-direction: column;
            gap: 0.1rem;

            .user {
                display: flex;
                align-items: center;
            }

            .level {
                color: var(--french-gray);
            }
        }
    }
</style>