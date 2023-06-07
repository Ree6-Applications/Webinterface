export const BASE_PATH = "https://147.135.69.14:8013"

export function get(path: string) {

    return fetch(BASE_PATH + path, {
        method: "GET",
        headers: {
            'X-Session-Authenticator': localStorage.getItem("token")!
        },
    })
}

export async function get_js(path: string) {

    const res = await fetch(BASE_PATH + path, {
        method: "GET",
        headers: {
            'X-Session-Authenticator': localStorage.getItem("token")!,
        },
    })

    if(res.status != 200) {
        console.error(res.status + " " + path)
        return {success: false}
    }

    const json = await res.json()
    if(!json.success) {
        console.error(json.message)
        return {success: false}
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
        return {success: false}
    }

    return json
}
