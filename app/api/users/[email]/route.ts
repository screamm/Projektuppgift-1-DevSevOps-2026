import { forwardJsonRequest } from "@/lib/backend";

type RouteContext = {
  params: Promise<{ email: string }>;
};

export async function GET(request: Request, context: RouteContext) {
  const { email } = await context.params;
<<<<<<< HEAD
  return forwardJsonRequest(
    request,
    `/api/users/${encodeURIComponent(email)}`,
  );
}

export async function DELETE(request: Request, context: RouteContext) {
  const { email } = await context.params;
  return forwardJsonRequest(
    request,
    `/api/users/${encodeURIComponent(email)}`,
  );
=======
  return forwardJsonRequest(request, `/api/users/${encodeURIComponent(email)}`);
}

export async function PATCH(request: Request, context: RouteContext) {
  const { email } = await context.params;
  return forwardJsonRequest(request, `/api/users/${encodeURIComponent(email)}`);
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
}
