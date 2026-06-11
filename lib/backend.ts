const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const bodylessRequestMethods = new Set(["GET", "HEAD"]);
const bodylessResponseStatuses = new Set([204, 205, 304]);

export async function forwardJsonRequest(request: Request, path: string) {
  try {
    const hasRequestBody = !bodylessRequestMethods.has(request.method);
    const response = await fetch(new URL(path, backendUrl), {
      method: request.method,
      headers: hasRequestBody
        ? { "content-type": "application/json" }
        : undefined,
      body: hasRequestBody ? await request.text() : undefined,
      cache: "no-store",
    });

    if (bodylessResponseStatuses.has(response.status)) {
      return new Response(null, { status: response.status });
    }

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
