import { forwardJsonRequest } from "@/lib/backend";

type RouteContext = {
  params: Promise<{ email: string }>;
};

export async function PUT(request: Request, context: RouteContext) {
  const { email } = await context.params;
  return forwardJsonRequest(
    request,
    `/api/users/${encodeURIComponent(email)}/password`,
  );
}
