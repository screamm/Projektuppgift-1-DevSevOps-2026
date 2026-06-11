const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

export async function forwardJsonRequest(request: Request, path: string) {
  try {
    const targetUrl = new URL(path, backendUrl);
    const requestUrl = new URL(request.url);

    requestUrl.searchParams.forEach((value, key) => {
      targetUrl.searchParams.set(key, value);
    });

    const method = request.method;
    const hasBody = method !== "GET" && method !== "HEAD";
    const body = hasBody ? await request.text() : undefined;

    const response = await fetch(targetUrl, {
      method,
      headers: hasBody ? { "content-type": "application/json" } : undefined,
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
