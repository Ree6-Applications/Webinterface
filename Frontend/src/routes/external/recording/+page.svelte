<script lang="ts">
    import { browser } from "$app/environment";
    import { page } from "$app/stores";
    import InfoPopup from "$lib/components/infoPopup.svelte";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { BASE_PATH, get, get_js } from "$lib/scripts/constants";
    import { onMount } from "svelte";
    import { fly, scale } from "svelte/transition";

    let loading = true;
    let found = true;
    
    let popup = false;
    let popupMessage = "";

    let recording: any = {};

    onMount(async () => {

        if(!$page.url.searchParams.has("id")) {
            loading = false;
            found = false;
            return;
        }

        const json = await get_js("/guilds/recording?recordId=" + $page.url.searchParams.get("id"));

        if(!json.success) {
            if(json.message === "Session not found!") {
                localStorage.setItem("redirect", $page.url.href);
                location.assign(BASE_PATH + "/auth/discord/request")
                return;
            }
            loading = false;
            found = false;
            return;
        }

        console.log(json)

        loading = false;
        recording = json.object;

    })
    
    function copy() {

        navigator.clipboard.writeText($page.url.href).then(() => {
            popupMessage = "Link copied to clipboard!";
            popup = true;
        }).catch(() => {
            popupMessage = "Failed to copy link to clipboard!";
            popup = true;
        });

        setTimeout(() => {
            popup = false;
        }, 1000);

    }
</script>

<svelte:head>
    <title>Recording</title>
</svelte:head>

{#if popup}
<InfoPopup title={popupMessage} content="" close={() => popup = false} />
{/if}

{#if loading}
<div class="body">
    <div out:scale class="transition">
        <LoadingIndicator size="100" />
    </div>
</div>

{:else if found}
<div class="body">
    <div in:fly={{y: 50, delay: 500}} class="record">
        <div class="title">
            <div class="column">
                <div class="user">
                    <img src={recording.creator.avatarUrl} alt="hi">
                    <h2 class="text-large">{recording.creator.name}</h2>
                </div>
                <p>Recorded on {new Date(parseInt(recording.creationTime)).toLocaleDateString("en-AU")}</p>
            </div>

            <div class="buttons">
                <div class="button" on:click={async () => {
                    const res = await get("/guilds/recording/download?recordId=" + $page.url.searchParams.get("id"));
                    const blob = await res.blob();

                    const url = window.URL.createObjectURL(blob);
                    const link = document.createElement('a');
                    link.href = url;
                    link.download = "recording.wav";
                    link.click();

                }} on:keydown>
                    <span class="material-icons icon-medium icon-primary clickable">download</span>
                    <p class="text-medium">Download</p>
                </div>
            </div>
        </div>
    </div>
</div>

{:else}
<div class="body">
    <span in:scale={{delay: 900}} class="material-icons colored found icon-primary" style="font-size: 80px;">search</span>
    <h2 in:fly={{y: 50, delay: 500}}>This recording doesn't exist!</h2>
</div>
{/if}

<style lang="scss">

    .record {
        display: flex;
        flex-direction: column;
        padding: 1rem;
        border-radius: 1rem;
        background-color: var(--onyx);
        width: 50%;
        min-width: min(85%, 500px);

        .title {
            display: flex;
            justify-content: space-between;

            p {
                color: var(--french-gray-2);
            }

            .user {
                display: flex;
                align-items: center;
                gap: 0.5rem;

                img {
                    width: 2.6rem;
                    height: 2.6rem;
                    border-radius: 50%;
                    object-fit: cover;
                }
            }
        }
    }

    .buttons {
        display: flex;
        gap: 0.7rem;
        align-items: center;

        .button {
            height: min-content;
            display: flex;
            align-items: center;
            gap: var(--button-gap);
            padding: var(--button-padding);
            background-color: var(--outer-space);
            border-radius: 1rem;
            transition: 250ms all ease;
            cursor: pointer;
            
            p {
                color: white;
            }

            &:hover {
                transform: scale(1.06);
            }
        }
    }

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

    .column {
        display: flex;
        justify-content: center;
        flex-direction: column;
        gap: 0.3rem;
    }

    .row {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 0.3rem;
    }
</style>