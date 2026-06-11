import { forwardJsonRequest } from "@/lib/backend";

type RouteContext = {
  params: Promise<{ email: string }>;
};

<<<<<<< HEAD
export async function PUT(request: Request, context: RouteContext) {
=======
export async function PATCH(request: Request, context: RouteContext) {
>>>>>>> ee89d5791ff178cc678277138af071e0a049a893
  const { email } = await context.params;
  return forwardJsonRequest(
    request,
    `/api/users/${encodeURIComponent(email)}/password`,
  );
}
