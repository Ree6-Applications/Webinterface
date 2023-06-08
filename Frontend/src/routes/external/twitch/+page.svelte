<script lang="ts">
    import { browser } from "$app/environment";
    import { page } from "$app/stores";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { BASE_PATH, get, get_js } from "$lib/scripts/constants";
    import { onMount } from "svelte";
    import { fly, scale } from "svelte/transition";

    let loading = true;
    let correct = false;
    let success = false;
    let message = '';

    onMount(async () => {
        correct = $page.url.searchParams.has("code");
        if(!correct) {
            location.assign(BASE_PATH + "/auth/twitch/request")
            return;
        }

        const code = $page.url.searchParams.get("code");

        try {
            const json = await get_js("/auth/twitch?code=" + code);

            message = json.message;
            
            if(!json.success) {
                if(json.message === "Session not found!") {
                    localStorage.setItem("redirect", $page.url.href);
                    location.assign(BASE_PATH + "/auth/discord/request")
                    return;
                }
                
                loading = false;
                success = false;
                return;
            } else {
                success = true;
            }
        } catch (e) {
            console.error(e);
        }

        loading = false;
    })
</script>

<svelte:head>
    <title>Twitch Autentication</title>
</svelte:head>

{#if loading}
<div class="body">
    <div out:scale class="transition">
        <LoadingIndicator size="100" />
    </div>
</div>

{:else if success}
<div class="body">
    <div in:fly={{y: 50, delay: 500}} class="column">
        <span in:scale={{delay: 900}} class="material-icons colored found icon-primary" style="font-size: 80px;">rocket</span>
        <h2>The Twitch authentication was successful.</h2>
        <p>You can close this tab now!</p>
    </div>
</div>

{:else}
<div class="body">
    <div in:fly={{y: 50, delay: 500}} class="column">
        <span in:scale={{delay: 900}} class="material-icons colored found icon-primary" style="font-size: 80px;">search</span>
        <h2>The Twitch authentication failed.</h2>
        <p>Reason for this is {message}</p>
    </div>
</div>
{/if}

<style lang="scss">

    .found {
        position: absolute;
        animation: found 10s infinite;
        text-shadow: 2px 2px 10px black;
    }

    @keyframes found {
        0% {
            transform: translate(-130%, -22%) scale(1);
        }
        20% {
            transform: translate(187%, 124%) scale(1.1);
        }
        40% {
            transform: translate(-170%, 82%) scale(0.9);
        }
        60% {
            transform: translate(-37%, -31%) scale(1.2);
        }
        80% {
            transform: translate(140%, 53%) scale(0.9);
        }
        100% {
            transform: translate(-130%, -22%) scale(1);
        }
    }

    .body {
        padding: 0;
        margin: 0;
        display: flex;
        height: 100vh;
        width: 100vw;
        background-color: var(--eerie-black);
        display: flex;
        align-items: center;
        justify-content: center;
    }
</style>