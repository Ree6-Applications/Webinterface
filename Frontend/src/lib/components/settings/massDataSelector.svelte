<script lang="ts">
    import { post_js } from "$lib/scripts/constants";
    import DataPopup from "../data_popup/dataPopup.svelte";
    import type { Model } from "../data_popup/popup";
    import LoadingIndicator from "../loadingIndicator.svelte";

    export let icon: string;
    export let title: string;
    export let endpoint: string;
    export let description: string;

    export let models: Model[] = [];
    
    let currentModel = 0;
    let picking = false;
    let loading = false;
    let error = false;

    function save() {

    }

    async function clear() {
        loading = true;
        const json = await post_js(endpoint + "/clear", "{}")
    
        if(!json.success) {
            error = true;
            setTimeout(() => {
                error = false;
                loading = false;
            }, 2000);
        } else {
            loading = false;
        }
    }

</script>

{#if picking}
<DataPopup title="Create {models[currentModel].name.toLowerCase()}" builder={() => {
    return models[currentModel].model;
}} action1="Create" action2="Cancel" action1Handler={(content) => {
    picking = false;
}} action2Handler={() => picking = false} close={() => picking = false}/>
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
    
        {#if !loading}
        <div class="button-bar">
            {#each models as model}
            <div class="button" on:click={() => {
                currentModel = models.indexOf(model);
                picking = true;
            }} on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">{model.primaryIcon}</span>
                <p class="text-small">{model.name}</p>
            </div>
            {/each}

            <div class="button" on:click={clear} on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">delete</span>
                <p class="text-small">Delete all</p>
            </div>
        </div>
        {:else}
        <LoadingIndicator error={error} size="45" />
        {/if}
    </div>
</div>

<style lang="scss">
    @import "$lib/styles/box.scss";
</style>