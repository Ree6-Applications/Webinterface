<script lang="ts">
    interface Props {
        size?: string;
        message?: string;
        error?: boolean;
    }

    let { size = "30", message = "Something went wrong!", error = false }: Props = $props();
</script>

<div class="loading">
    {#if error}
    <span class="material-icons icon-primary error" style="font-size: {size}px;">warning</span>
    <p class="text-medium">{message}</p>
    {:else}
    <span class="material-icons icon-primary spinner" style="font-size: {size}px;">hourglass_empty</span>
    {/if}
</div>

<style lang="scss">

    .loading {
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .spinner {
        animation: loading 2s cubic-bezier(.57,-0.48,.29,1.43) infinite;
    }

    .error {
        color: orange;
        animation: error 2s ease-in-out infinite;
    }

    @keyframes loading {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }

    @keyframes error {
        from { transform: translateY(-5px); }
        50% { transform: translateY(5px); }
        to { transform: translateY(-5px); }
    }

</style>
