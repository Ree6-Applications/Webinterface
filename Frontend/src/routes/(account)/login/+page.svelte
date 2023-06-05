<script lang="ts">
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { BASE_PATH } from "$lib/scripts/constants";
    import { goto } from "$app/navigation";

    let loading = true;
    let message = '';
    let correct = false;

    onMount(async () => {
        correct = $page.url.searchParams.has("code") && $page.url.searchParams.has("state");
        if(!correct) return;

        const code = $page.url.searchParams.get("code");
        const state = $page.url.searchParams.get("state");

        try {
            const res = await fetch(BASE_PATH + "/auth/discord?code=" + code + "&state=" + state, {
                method: "GET",
            })

            const json = await res.json();

            if(json.success) {
                localStorage.setItem("token", json.object.identifier);
                localStorage.setItem("avatar", json.object.user.avatarUrl);
                localStorage.setItem("name", json.object.user.name);
                localStorage.setItem("id", json.object.user.id);
                localStorage.setItem("discriminator", json.object.user.discriminator);

                goto("/dash")
            }
            
            message = json.message;

        } catch (e) {
            console.error(e);
        }

        loading = false;
    })

</script>

<div class="box">
    {#if loading}
    <h2>Loading..</h2>

    {:else if !correct}
    <h2>Invalid request.</h2>
    {:else}
    <h2>{message}</h2>
    {/if}
</div>

<style lang="scss">

    .box {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        padding: 1rem;
        border-radius: 0.5rem;
        background-color: var(--eerie-black);
    }

</style>
