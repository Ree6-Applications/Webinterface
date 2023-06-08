<script lang="ts">
    import { onMount } from "svelte";
    import { goto } from "$app/navigation";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { fly, scale, slide } from "svelte/transition";

    onMount(async () => {
        goto("/dash");
    })
</script>

<svelte:head>
    <title>Loading...</title>
</svelte:head>

<div class="body">
    <div out:scale class="center">
        <LoadingIndicator size="100" />
    </div>
</div>

<style lang="scss">

    .center {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .body {
        padding: 0;
        margin: 0;
        display: flex;
        height: 100vh;
        width: 100vw;
        background-color: var(--onyx);
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .column {
        display: flex;
        justify-content: center;
        align-items: center;
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