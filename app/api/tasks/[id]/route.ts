import { forwardJsonRequest } from "@/lib/backend";

type RouteContext = {
  params: Promise<{ id: string }>;
};

export async function PUT(request: Request, { params }: RouteContext) {
  const { id } = await params;
  return forwardJsonRequest(request, `/api/tasks/${encodeURIComponent(id)}`);
}

export async function DELETE(request: Request, { params }: RouteContext) {
  const { id } = await params;
  return forwardJsonRequest(request, `/api/tasks/${encodeURIComponent(id)}`);
}
