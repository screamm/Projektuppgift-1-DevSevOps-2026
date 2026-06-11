const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

export async function forwardJsonRequest(request: Request, path: string) {
  try {
    const body =
      request.method === "GET" || request.method === "HEAD"
        ? undefined
        : await request.text();
    const response = await fetch(new URL(path, backendUrl), {
      method: request.method,
      headers: {
        "content-type": "application/json",
      },
      body,
      cache: "no-store",
    });

    return new Response(await response.text(), {
      status: response.status,
      headers: {
        "content-type":
          response.headers.get("content-type") ?? "application/json",
      },
    });
  } catch {
    return Response.json(
      { message: "Could not connect to the backend service." },
      { status: 502 },
    );
  }
}
