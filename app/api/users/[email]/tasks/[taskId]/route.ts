import { forwardJsonRequest } from "@/lib/backend";

type RouteContext = {
  params: Promise<{ email: string; taskId: string }>;
};

export async function PUT(request: Request, context: RouteContext) {
  const { email, taskId } = await context.params;
  return forwardJsonRequest(
    request,
    `/api/users/${encodeURIComponent(email)}/tasks/${encodeURIComponent(taskId)}`,
  );
}
