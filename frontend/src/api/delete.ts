// filepath: src/api/delete.ts

export const deleteHistoryItem = async (id: number): Promise<void> => {
  const response = await fetch(`/api/history/${id}/delete`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    throw new Error(`Failed to delete history item with ID ${id}`);
  }
};

export const deleteAllHistory = async (): Promise<void> => {
  const response = await fetch(`/api/history/delete-all?confirm=YES`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    throw new Error(`Failed to delete all history: ${response.status}`);
  }
};

export const deleteHistoryByStatus = async (status: string): Promise<void> => {
  const response = await fetch(`/api/history/delete?status=${status}`, {
    method: 'DELETE',
  });

  if (!response.ok) {
    throw new Error(`Failed to delete history by status: ${response.status}`);
  }
};