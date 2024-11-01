<script lang="ts">
    import { createBubbler } from 'svelte/legacy';

    const bubble = createBubbler();
    import { get_js, post_js } from "$lib/scripts/constants";
    import { onMount } from "svelte";
    import DataPopup from "../data_popup/dataPopup.svelte";
    import { model2JSON, type Model } from "../data_popup/popup";
    import LoadingIndicator from "../loadingIndicator.svelte";
    import ConfirmPopup from "../confirmPopup.svelte";
    import { slide } from "svelte/transition";


    interface Props {
        icon: string;
        title: string;
        endpoint: string;
        description: string;
        deleteField?: (json: any) => string;
        deleteJson?: (json: any) => any;
        models?: Model[];
    }

    let {
        icon,
        title,
        endpoint,
        description,
        deleteField = (json) => "",
        deleteJson = (json) => {},
        models = []
    }: Props = $props();
    
    let currentModel = $state(0);
    let picking = $state(false);
    let loading = $state(true);
    let error = $state(false);
    let objectsLoaded = false;

    let confirm = $state(false);
    let objects: any[] = $state([]);

    onMount(() => reload())

    async function reload() {
        loading = true;
        objects = [];
        const json = await get_js(endpoint)

        if(!json.success) {
            error = true;
            return;
        }

        console.log(json)
        loading = false;
        json.object.forEach((element: any) => {

            const model = modelForObject(element)

            objects.push({
                object: element,
                model: model
            })
        })


        objectsLoaded = true;
    }

    async function clear() {
        confirm = true;
        loading = true;
    }

    function modelForObject(object: any): Model | undefined {
        for(let model of models) {
            if(model.isModel(object)) {
                return model;
            }
        }

        return undefined;
    }

</script>

{#if picking}
<DataPopup title="Create {models[currentModel].name.toLowerCase()}" builder={() => {
    return structuredClone(models[currentModel].model);
}} action1="Create" action2="Cancel" action1Handler={async (content) => {
    picking = false;
    
    const body = model2JSON(content);
    console.log(body)

    loading = true;
    const json = await post_js(endpoint + "/add", body);

    if(!json.success) {
        loading = true;
        error = true;
        setTimeout(() => {
            loading = false;
            error = false;
        }, 2000);
        return;
    }

    loading = false;
    reload();    

}} action2Handler={() => picking = false} close={() => picking = false}/>
{/if}

{#if confirm}
<ConfirmPopup title="Confirm deletion." content="Do you really want to delete everything in this list? This action cannot be undone!" 
close={async (b) => {
    confirm = false;
    if(b) {
        const json = await post_js(endpoint + "/clear", "{}")
    
        if(!json.success) {
            error = true;
            setTimeout(() => {
                error = false;
                loading = false;
            }, 2000);
        } else {
            loading = false;
            objects = [];
        }
    } else {
        loading = false;
    }

}} />
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
            <div class="button" onclick={() => {
                currentModel = models.indexOf(model);
                picking = true;
            }} onkeydown={() => {}}>
                <span class="material-icons icon-small icon-primary">add</span>
                <p class="text-small">{model.name}</p>
            </div>
            {/each}

            <div class="button" onclick={clear} onkeydown={() => {}}>
                <span class="material-icons icon-small icon-primary">delete</span>
                <p class="text-small">Delete all</p>
            </div>
        </div>
        {:else}
        <LoadingIndicator error={error} size="45" />
        {/if}
    </div>

    {#if !loading && objects.length > 0}
    <div in:slide class="content default-margin">
        <div class="models">

            {#each objects as object}
            <div class="model">
                <div class="title">
                    <span class="material-icons icon-small icon-primary">{object.model.primaryIcon}</span>
                    <p class="text-small">{object.model.renderFormat(object.object)}</p>
                </div>                
                <span onclick={async () => {
                    
                    loading = true;

                    const requestJson = deleteField(object.object) != "" ? JSON.stringify({
                        "value": deleteField(object.object)
                    }) : JSON.stringify(deleteJson(object.object));

                    const json = await post_js(endpoint + "/remove", requestJson)

                    if(!json.success) {
                        setTimeout(() => {
                            error = true;
                            loading = false;
                        }, 2000);
                        return;
                    }

                    loading = false;
                    reload();

                }} onkeydown={bubble('keydown')} class="material-icons clickable icon-primary icon-small">delete</span>
            </div>
            {/each}

        </div>
    </div>
    {/if}
</div>

<style lang="scss">
    @import "$lib/styles/box.scss";
    @import "$lib/default.scss";

    .models {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        padding: 0.2rem;

        .model {
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-radius: 0.5rem;
            background-color: var(--onyx);
            padding: 0.5rem;

            .title {
                display: flex;
                align-items: center;
                gap: 0.5rem;
            }
        }
    }
</style>