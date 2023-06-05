<script lang="ts">
    import { page } from "$app/stores";
    import MassBoolean from "$lib/components/settings/massBoolean.svelte";
    import MassStringSelector from "$lib/components/settings/massStringSelector.svelte";
    import StringSelector from "$lib/components/settings/stringSelector.svelte";

    
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

<MassStringSelector icon="block" title="Blacklisted roles" description="Any message containing a word from the list of blacklisted words will be deleted and logged." endpoint={"/guilds/" + $page.params.serverId + "/blacklist"} />

<h1 class="headline">Command settings</h1>

<div class="default-margin"></div>

<MassBoolean icon="keyboard_command_key" title="Enabled commands" description="Configure all commands that can be executed using the discord bot." prefix="command_" />

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