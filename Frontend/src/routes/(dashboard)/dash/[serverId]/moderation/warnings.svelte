<script lang="ts">
    import { page } from "$app/stores";
    import ConfirmPopup from "$lib/components/confirmPopup.svelte";
    import LoadingIndicator from "$lib/components/loadingIndicator.svelte";
    import { get_js, post_js } from "$lib/scripts/constants";
    import { onMount } from "svelte";
    import { slide } from "svelte/transition";

    let loading = true
    let error = false

    let confirm = false
    let confirmMessage = "", confirmTitle = ""
    let currentAction = ""
    let currentTarget = ""
    let warnings: any[] = []
    let setCallback: Function = () => {}

    onMount(async () => {
        const json = await get_js("/guilds/" + $page.params.serverId + "/warnings")
        
        if(!json.success) {
            error = true
            return
        }
        
        warnings = json.object

        console.log(json)
        loading = false;
    })

    function addWarning(id: string, callback: Function) {
        confirm = true;
        setCallback = callback
        confirmTitle = "Add warning"
        confirmMessage = "Do you really want to add a warning to this user?"
        currentAction = "add"
        currentTarget = id + ":1"
    }

    function removeWarning(id: string, callback: Function) {
        confirm = true;
        setCallback = callback
        confirmTitle = "Remove warning"
        confirmMessage = "Do you really want to remove a warning to this user?"
        currentAction = "add"
        currentTarget = id + ":-1"
    }

    function clearWarningsOfUser(id: string, callback: Function) {
        confirm = true;
        setCallback = callback
        confirmTitle = "Clear warnings"
        confirmMessage = "Do you really want to clear ALL warnings of this user? This action cannot be undone."
        currentAction = "remove"
        currentTarget = id
    }

    function clearWarnings() {
        confirm = true;
        confirmTitle = "Clear warnings"
        confirmMessage = "Do you really want to clear all warnings? This action cannot be undone."
        currentAction = "clear"
    }

</script>

{#if confirm}
<ConfirmPopup title={confirmTitle} content={confirmMessage} close={async (b) => {
    confirm = false;

    if(b) {

        let json;
        switch(currentAction) {

            case "add":

                loading = true;
                const args = currentTarget.split(":")
                json = await post_js("/guilds/" + $page.params.serverId + "/warnings/add", JSON.stringify({
                    "userId": args[0],
                    "warnings": args[1]
                }))

                console.log(json)

                if(!json.success) {
                    setTimeout(() => {
                        error = true
                        loading = false;
                    }, 2000);
                } else {
                    loading = false;
                    setCallback()
                }

                break;

            case "remove":

                loading = true;
                json = await post_js("/guilds/" + $page.params.serverId + "/warnings/remove", JSON.stringify({
                    "value": currentTarget,
                }))

                console.log(json)

                if(!json.success) {
                    setTimeout(() => {
                        error = true
                        loading = false;
                    }, 2000);
                } else {
                    loading = false;
                    setCallback()
                }

                break;

            case "clear":

                loading = true;
                json = await post_js("/guilds/" + $page.params.serverId + "/warnings/clear", "{}")

                if(!json.success) {
                    setTimeout(() => {
                        error = true
                        loading = false;
                    }, 2000);
                } else {
                    warnings = []
                    loading = false;
                }

                break;
        }
    }

    currentTarget = ""
}} />
{/if}

<div class="box default-margin">
    <div class="box-title">
        <div class="content">
            <div class="title">
                <span class="material-icons icon-primary icon-small">crisis_alert</span>
                <h1 class="text-medium">Warnings</h1>
            </div>
            <p class="text-bg">The list of all users that have been warned.</p>
        </div>

        {#if !loading}
        <div class="button-bar ns">

            {#if warnings.length > 0}
            <div class="button" on:click={() => clearWarnings()} on:keydown={() => {}}>
                <span class="material-icons icon-small icon-primary">delete</span>
                <p class="text-small">Delete all</p>
            </div>
            {:else}
            <p class="text">No warnings found!</p>
            {/if}
        </div>
        {:else}
        <LoadingIndicator error={error} size="43" />
        {/if}

    </div>

    {#if (!loading || currentTarget != "") && warnings.length > 0}
    <div in:slide class="chips default-margin">
        {#each warnings as warning}

        <div class="chip">
            <img src={warning.user.avatarUrl} alt="User avatar" />
            <p class="text-small">{warning.user.name}</p>

            <div class="hr-v"></div>

            {#if !currentTarget.includes(warning.user.id)}
            <span class="material-icons icon-small icon-primary clickable" on:click={() => addWarning(warning.user.id, () => {
                warning.warnings = (parseInt(warning.warnings) + 1).toString()
            })} on:keydown>add</span>
            <p class="text-small">{warning.warnings}</p>
            <span class="material-icons icon-small icon-primary clickable" on:click={() => removeWarning(warning.user.id, () => {
                warning.warnings = (parseInt(warning.warnings) - 1).toString()
            })} on:keydown>remove</span>
            <div class="hr-v"></div>
            <span class="material-icons icon-small icon-primary clickable" on:click={() => clearWarningsOfUser(warning.user.id, () => {
                warnings = warnings.filter((w) => w.user.id != warning.user.id)
            })} on:keydown>delete</span>
            {:else}
            <LoadingIndicator size="25" />
            {/if}
        </div>

        {/each}
    </div>
    {/if}

</div>

<style lang="scss">
    @import '$lib/styles/box.scss';
    @import '$lib/default.scss';
    @import '$lib/styles/chips.scss';

    img {
        height: 1.9rem;
        border-radius: 10rem;
    }

</style>