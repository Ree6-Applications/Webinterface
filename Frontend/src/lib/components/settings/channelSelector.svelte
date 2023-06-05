<script lang="ts">
    import { onMount } from "svelte";
    import LoadingIndicator from "../loadingIndicator.svelte";
    import { get, post, post_js } from "$lib/scripts/constants";
    import type { Channel } from "$lib/scripts/servers";
    import ChannelPicker from "../channelPicker.svelte";

    export let title: string;
    export let windowTitle: string = "Select a channel.";
    export let icon: string;
    export let description: string;
    export let endpoint: string;

    let channelPicker = false;
    let loading = false;
    let error = false;
    let current: Channel = {
        id: ":loading",
        name: "Loading...",
        type: "TEXT"
    }

    onMount(async () => {
        loading = true;
        
        // Request value
        const res = await get(endpoint)
        console.log(endpoint)
        if(res.status != 200) {
            error = true;
            return;
        }

        // Set value
        const json = await res.json()
        console.log(json)
        if(!json.success) {
            error = true;
            return;
        }

        current = {
            id: json.object.id,
            name: json.object.name,
            type: "TEXT"
        }

        loading = false;
        error = false;
    })

</script>

{#if channelPicker}
<ChannelPicker message={windowTitle} callback={async (channel) => {    
    channelPicker = false
    loading = true;

    if(channel.id == "-1") {

        if(current.id != null) {
            current = {
                id: null,
                name: null,
                type: "TEXT"
            }
            
            // Remove channel
            await post_js(endpoint + "/remove", "{}")
        }

    } else {
        current = channel;

        console.log(JSON.stringify({
            "value": channel.id?.toString()
        }));

        return;

        // Set channel
        await post_js(endpoint + "/add", JSON.stringify({
            "value": channel.id?.toString()
        }))
    }

    loading = false;
}} current={current} />
{/if}

<div class="box default-margin">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">{icon}</span>
                <h1 class="text-medium">{title}</h1>
            </div>
            <p class="text-bg">{description}</p>
        </div>

        {#if loading}
        <div class="loading">
            <LoadingIndicator error={error} size="45" />
        </div>
        {:else}
        <div class="button-bar">

            {#if current.id != null}
            <div class="text">
                <span class="material-icons icon-primary icon-small">tag</span>
                <p>{current.name}</p>
            </div>
            {:else}
            <div class="text">
                <span class="material-icons icon-primary icon-small">close</span>
                <p>Nothing</p>
            </div>
            {/if}

            <div on:click={() => channelPicker = true} on:keydown class="button icon-button">
                <span class="material-icons icon-small icon-primary">edit</span>
            </div>
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
</style>
