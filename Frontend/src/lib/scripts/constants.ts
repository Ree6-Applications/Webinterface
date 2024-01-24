export const BASE_PATH = import.meta.env.VITE_API_URL || "https://api.ree6.de"
export const INVITE_URL = import.meta.env.VITE_INVITE_URL || "https://invite.ree6.de"

export function get(path: string) {

    return fetch(BASE_PATH + path, {
        method: "GET",
        headers: {
            'X-Session-Authenticator': localStorage.getItem("token")!
        },
    })
}

export async function get_js(path: string) {

    let res;
    try {
        res = await fetch(BASE_PATH + path, {
            method: "GET",
            headers: {
                'X-Session-Authenticator': localStorage.getItem("token")!,
            },
        })
    } catch(e) {
        console.error(e)
        return {success: false}
    }

    if(res?.status != 200) {
        console.error(res?.status + " " + path)
        return {success: false}
    }

    const json = await res.json()
    if(!json.success) {
        console.error(json.message)
        return json
    }

    return json
}

export function post(path: string, body: string) {

    return fetch(BASE_PATH + path, {
        method: "POST",
        body: body,
        headers: {
            'X-Session-Authenticator': localStorage.getItem("token")!,
            "Content-Type": "application/json"
        },
    })
}

export async function post_js(path: string, body: string) {


    const res = await fetch(BASE_PATH + path, {
        method: "POST",
        body: body,
        headers: {
            'X-Session-Authenticator': localStorage.getItem("token")!,
            "Content-Type": "application/json"
        },
    })

    if(res.status != 200) {
        console.error(res.status + " " + path)
        return {success: false}
    }

    const json = await res.json()
    if(!json.success) {
        console.error(json.message)
        return {success: false, message: json.message}
    }

    return json
}
