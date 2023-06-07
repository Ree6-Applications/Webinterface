<script lang="ts">
    import { page } from "$app/stores";
    import DataPopup from "$lib/components/data_popup/dataPopup.svelte";
    import MassBoolean from "$lib/components/settings/massBoolean.svelte";
    import MassDataSelector from "$lib/components/settings/massDataSelector.svelte";
    import MassStringSelector from "$lib/components/settings/massStringSelector.svelte";
    import StringSelector from "$lib/components/settings/stringSelector.svelte";
    import { currentServer } from "$lib/scripts/servers";
    import Warnings from "./warnings.svelte";

    
    let blacklist = ["Glatze", "Bastard"]

    let wordToAdd: string = ""

    function addWord() {
        blacklist.push(wordToAdd)
        blacklist = blacklist
        wordToAdd = ""
    }

    function removeWord(toRemove: string) {
        blacklist = blacklist.filter(word => word != toRemove)
        blacklist = blacklist
    }

</script>

<h1 class="headline">Moderation settings</h1>

<StringSelector icon="layers" title="Command prefix" description="Select the prefix for all commands." settingName="chatprefix" />

<div class="default-margin"></div>

<MassStringSelector icon="block" title="Blacklisted words" description="Any message containing a word from the list of blacklisted words will be deleted and logged." endpoint={"/guilds/" + $page.params.serverId + "/blacklist"} />

<h1 class="headline">Command settings</h1>

<div class="default-margin"></div>

<MassBoolean icon="keyboard_command_key" title="Enabled commands" description="Configure all commands that can be executed using the discord bot." prefix="command_" />

<h1 class="headline">Warnings & punishments</h1>

<div class="default-margin"></div>

<Warnings />

<div class="default-margin"></div>

<MassDataSelector icon="auto_awesome" title="Automatic punishments" description="Automatically do certain things when a user has more than a certain amount of warnings."
    models={[
        {
            name: "Timeout",
            primaryIcon: "timelapse",
            primaryIndex: 0,
            jsonName: "type",
            jsonInserter: "timeout",
            model: [
                {
                    name: "Needed warnings",
                    jsonName: "neededWarnings",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "warnings",
                },
                {
                    name: "action",
                    jsonName: "action",
                    type: "string",
                    value: "timeout",
                    visible: false,
                    unit: ""
                },
                {
                    name: "Timeout length",
                    jsonName: "timeoutTime",
                    type: "int",
                    value: 1000,
                    visible: true,
                    unit: "seconds"
                },
                {
                    name: "role",
                    jsonName: "roleId",
                    type: "role",
                    value: null,
                    visible: false,
                    unit: ""
                },
            ]
        },
        {
            name: "Role",
            primaryIcon: "timelapse",
            primaryIndex: 0,
            jsonName: "type",
            jsonInserter: "timeout",
            model: [
                {
                    name: "Needed warnings",
                    jsonName: "neededWarnings",
                    type: "int",
                    value: 1,
                    visible: true,
                    unit: "warnings",
                },
                {
                    name: "action",
                    jsonName: "action",
                    type: "string",
                    value: "role",
                    visible: false,
                    unit: ""
                },
                {
                    name: "Timeout length",
                    jsonName: "timeoutTime",
                    type: "int",
                    value: 0,
                    visible: false,
                    unit: "seconds"
                },
                {
                    name: "Role",
                    jsonName: "roleId",
                    type: "role",
                    value: null,
                    visible: true,
                    unit: ""
                },
            ]
        }
    ]}
endpoint={"/guilds/" + $page.params.serverId + "/warnings/punishments"}/>

<style lang="scss">
    @import '$lib/default.scss';
    @import '$lib/styles/box.scss';
    @import '$lib/styles/chips.scss';

    .chip-button {
        margin-left: 0.1rem;
        color: var(--seasalt);
        transition: 250ms ease;

        &:hover {
            color: var(--primary);
        }
    }

</style>