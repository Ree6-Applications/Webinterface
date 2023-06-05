<div class="body">

    <div class="column">
        <span class="material-icons colored hammer icon-primary" style="font-size: 80px;">hardware</span>
        <h2 class="hammered">Coming soon..</h2>
    </div>
</div>

<style lang="scss">

    .hammer {
        animation: hammering 1s infinite;
    }

    .hammered {
        animation: hammered 1s infinite;
    }

    @keyframes hammering {
        0% {
            transform: rotate(0deg) translateX(10%) scaleY(1);
        }

        20% {
            transform: rotate(90deg) translateX(23%) scaleY(1);
        }

        25% {
            transform: rotate(90deg) translateX(23%) scaleY(1.05);
        }

        100% {
            transform: rotate(0deg) translateX(10%) scaleY(1);
        }
    }

    @keyframes hammered {
        0% {
            transform: scale(1);
        }

        27% {
            transform: scale(1);
        }

        40% {
            transform: scale(0.9);
        }

        100% {
            transform: scale(1);
        }
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