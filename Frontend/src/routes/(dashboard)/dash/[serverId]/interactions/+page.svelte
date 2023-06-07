<script lang="ts">
    import { page } from "$app/stores";
    import ChannelSelector from "$lib/components/settings/channelSelector.svelte";
    import MassBoolean from "$lib/components/settings/massBoolean.svelte";
    import MassRoleSelector from "$lib/components/settings/massRoleSelector.svelte";
    import MessageSelector from "$lib/components/settings/messageSelector.svelte";
    import { currentServer } from "$lib/scripts/servers";

</script>

<svelte:head>
    <title>Interactions - { $currentServer.name }</title>
</svelte:head>

<h1 class="headline">Tickets</h1>

<ChannelSelector icon="tag" title="Logging channel" description="Select the channel for all log messages." endpoint={"/guilds/" + $page.params.serverId + "/tickets"} />

<h1 class="headline">Suggestions</h1>

<ChannelSelector icon="tag" title="Suggestions channel" description="Select the channel for all suggestions." endpoint={"/guilds/" + $page.params.serverId + "/suggestions"} />

<div class="default-margin"></div>

<MessageSelector icon="mail" title="Suggestions Embed Message" description="Configure message that is being shown in the Suggestions Embed." settingName="message_suggestion_menu"/>

<h1 class="headline">Temporal Voicechannel</h1>

<ChannelSelector icon="tag" title="Temporal Voicechannel" description="Select the channel thats creates new temporal Voicechannel." endpoint={"/guilds/" + $page.params.serverId + "/temporalvoice"} />

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