<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import { onMount } from "svelte";
    import LoadingIndicator from "../loadingIndicator.svelte";
    import { get, post, post_js } from "$lib/scripts/constants";
    import type { Channel } from "$lib/scripts/servers";
    import ChannelPicker from "../channelPicker.svelte";

    interface Props {
        title: string;
        windowTitle?: string;
        icon: string;
        type?: string;
        description: string;
        endpoint: string;
    }

    let {
        title,
        windowTitle = "Select a channel.",
        icon,
        type = "TEXT",
        description,
        endpoint
    }: Props = $props();

    let channelPicker = $state(false);
    let loading = $state(false);
    let error = $state(false);
    let current: Channel = $state({
        id: ":loading",
        name: "Loading...",
        type: type
    })

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
            type: type
        }

        loading = false;
        error = false;
    })

</script>

{#if channelPicker}
<ChannelPicker message={windowTitle} type={type} callback={async (channel) => {    
    channelPicker = false
    if(channel == undefined) return;
    loading = true;

    if(channel.id == "-1") {

        if(current.id != null) {
            current = {
                id: null,
                name: null,
                type: type
            }
            
            // Remove channel
            await post_js(endpoint + "/remove", "{}")
        }

    } else {
        current = channel;

        console.log(JSON.stringify({
            "value": channel.id?.toString()
        }));

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
                <span class="material-icons icon-primary icon-small">{type == "TEXT" ? "tag" : "graphic_eq"}</span>
                <p>{current.name}</p>
            </div>
            {:else}
            <div class="text">
                <span class="material-icons icon-primary icon-small">close</span>
                <p>Nothing</p>
            </div>
            {/if}

            <div onclick={() => channelPicker = true} onkeydown={bubble('keydown')} class="button icon-button">
                <span class="material-icons icon-small icon-primary">edit</span>
            </div>
        </div>
        {/if}
    </div>
</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
</style>
