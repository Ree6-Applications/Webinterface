# Ree6 Frontend

## Netlify

If you don't have your own hosting you'd like to use we recommend using [Netlify](https://www.netlify.com/).
It's free, fast and easy to setup.

To use it [follow the instructions in our Wiki](https://wiki.ree6.de/webinterface/self-hosting).

## Docker

If you'd like to use your own hosting `Docker` + `Docker Compose` is the second best choice.
To familiarize yourself with `Docker Compose` you can look read through Docker's
[Try Docker Compose](https://docs.docker.com/compose/gettingstarted/)
manual.

We provide a [basic `docker-compose.yml` file](https://github.com/Ree6-Applications/Webinterface/tree/master/Frontend/docker/docker-compose.yml)
for easier setup.

Don't forget to change `BACKEND_URL` and `INVITE_URL` to your values. 

## Manual build

This web app is built using [Swelte](https://svelte.dev/).

Please keep in mind that Swelte can sometimes be hader to build so we recommend using one of the above options.

Below is a README for Swelte applications:

<details>
<summary>Swelte README</summary>

# create-svelte

Everything you need to build a Svelte project, powered by [`create-svelte`](https://github.com/sveltejs/kit/tree/master/packages/create-svelte).

## Creating a project

If you're seeing this, you've probably already done this step. Congrats!

```bash
# create a new project in the current directory
npm create svelte@latest

# create a new project in my-app
npm create svelte@latest my-app
```

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```bash
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://kit.svelte.dev/docs/adapters) for your target environment.

</details>
